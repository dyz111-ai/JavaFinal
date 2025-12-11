package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.AddCopiesDto;
import com.example.demo0.admin.dto.BookAdminDto;
import com.example.demo0.admin.dto.CreateBookDto;
import com.example.demo0.admin.dto.UpdateBookDto;
import com.example.demo0.admin.dto.UpdateBookLocationDto;
import com.example.demo0.admin.service.BookAdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/admin/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "LIBRARIAN"}) // 限制只有管理员和图书管理员角色可以访问
public class BookAdminController {

    @Inject
    private BookAdminService bookAdminService;

    @GET
    public Response getBooks(@QueryParam("search") String search) {
        if (search == null) search = "";
        List<BookAdminDto> books = bookAdminService.getBooks(search);
        return Response.ok(books).build();
    }

    @POST
    public Response createBook(CreateBookDto dto) {
        try {
            bookAdminService.createBook(dto);
            return Response.ok(new SimpleMessage("Book and its copies created successfully.")).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new SimpleMessage(ex.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{isbn}")
    public Response updateBook(@PathParam("isbn") String isbn, UpdateBookDto dto) {
        boolean success = bookAdminService.updateBookInfo(isbn, dto);
        if (!success) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }

    @PUT
    @Path("/{isbn}/location")
    @RolesAllowed({"ADMIN", "LIBRARIAN"})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBookLocation(@PathParam("isbn") String isbn, @Valid UpdateBookLocationDto locationDto) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"图书ISBN不能为空\"}")
                        .build();
            }
            
            if (locationDto == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"位置信息不能为空\"}")
                        .build();
            }
            
            // 获取完整位置字符串
            String location = locationDto.getLocation();
            if (location == null || location.trim().isEmpty()) {
                // 如果没有提供完整位置，尝试根据各部分构建
                StringBuilder locationBuilder = new StringBuilder();
                if (locationDto.getBuilding() != null) {
                    locationBuilder.append(locationDto.getBuilding());
                }
                if (locationDto.getFloor() != null) {
                    locationBuilder.append(locationDto.getFloor());
                }
                if (locationDto.getZone() != null) {
                    locationBuilder.append(locationDto.getZone());
                }
                if (locationDto.getShelf() != null) {
                    locationBuilder.append(locationDto.getShelf());
                }
                location = locationBuilder.toString();
            }
            
            boolean success = bookAdminService.updateBookLocation(isbn, location);
            if (success) {
                return Response.ok("{\"message\": \"图书位置更新成功\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"未找到可更新位置的图书\"}")
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"更新图书位置失败\"}")
                    .build();
        }
    }
    
    @PUT
    @Path("/{isbn}/takedown")
    @RolesAllowed({"ADMIN", "LIBRARIAN"})
    public Response takedownBook(@PathParam("isbn") String isbn) {
        boolean success = bookAdminService.takedownBook(isbn);
        if (!success) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new SimpleMessage("No available copies found to takedown for this ISBN."))
                    .build();
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/copies")
    public Response addCopies(AddCopiesDto dto) {
        try {
            bookAdminService.addCopies(dto);
            return Response.ok(new SimpleMessage("新副本已成功入库。")).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new SimpleMessage(ex.getMessage()))
                    .build();
        }
    }

    // 内部类用于返回简单消息
    private static class SimpleMessage {
        private String message;

        public SimpleMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}