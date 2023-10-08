package br.com.fiap.domain.resources;

import br.com.fiap.domain.service.Service;
import br.com.fiap.domain.service.ServiceService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Path("pf/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceResource implements Resource<Service, Long> {
    @Context
    UriInfo uriInfo;

    ServiceService service = new ServiceService();
    @GET
    @Override
    public Response findAll() {
        List<Service> all = service.findAll();
        return Response.ok( all ).build();
    }
    @GET
    @Path("/{id}")
    @Override
    public Response findById(@PathParam("id") Long id) {

        Service servico = service.findById( id );

        if (Objects.isNull( servico )) return Response.status( 404 ).build();

        return Response.ok( servico ).build();
    }

    @POST
    @Override
    public Response persiste(Service service) {
        service.setId( null );
        Service pessoa = service.persiste(service);

        if (Objects.isNull( pessoa.getId() ))
            return Response.notModified( "Não foi possível persistir: " + service ).build();

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        URI uri = uriBuilder.path( String.valueOf( pessoa.getId() ) ).build();

        return Response.created( uri ).entity( pessoa ).build();

    }
}
