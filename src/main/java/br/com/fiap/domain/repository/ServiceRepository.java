package br.com.fiap.domain.repository;

import br.com.fiap.domain.service.Service;
import br.com.fiap.infra.ConnectionFactory;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static br.com.fiap.domain.repository.AnimalRepository.fecharObjetos;

public class ServiceRepository implements Repository<Service, Long> {
    private ConnectionFactory factory;
    private static final AtomicReference<ServiceRepository> instance = new AtomicReference<>();
    private ServiceRepository(String nome, String tipo, String descricao){
        this.factory = ConnectionFactory.build();
    }
    public static ServiceRepository build(){
        instance.compareAndSet(null, new ServiceRepository(nome, tipo, descricao));
        return instance.get();
    }
    @Override
    public List<Service> findAll() {
        List<Service> list = new ArrayList<>();
        Connection con = factory.getConnection();
        ResultSet rs = null;
        Statement st = null;
        try {
            String sql = "SELECT * FROM TB_SERVICE";
            st = con.createStatement();
            rs = st.executeQuery(sql);
            if (rs.isBeforeFirst()){
                while (rs.next()){
                Long id = rs.getLong("ID_SERVICO");
                String descricao = rs.getString("DESCRICAO_SERVICO");
                String tipo = rs.getString("TIPO_SERVICO");
                list.add(new Service(id, descricao, tipo) {
                });
                }
            }
        }catch (SQLException e ){
            System.err.println("Não foi possível consultar os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(rs, st, con);
        }
        return list;
    }

    @Override
    public Service findById(Long id) {
        Service service = null;
        var sql = "SELECT * FROM TB_SERVICE where ID_SERVICE = ?";
        Connection con = factory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    String nome = rs.getString("NOME_SERVICO");
                    String tipo = rs.getString("TIPO_SERVICO");
                    String descricao = rs.getString("DESCRICAO_SERVICO");
                    service = new Service(nome, tipo, descricao);
                }
            }else {
                System.out.println("Não foi possível consultar os dados!\n" + id);
            }
        }catch (SQLException e){
            System.err.println("Não foi possível consultar os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(rs, ps, con);
        }
        return service;
    }

    @Override
    public Service persiste(Service service) {

        var sql = "BEGIN INSERT INTO TB_SERVICE (NOME_SERVICE, TIPO_SERIVCE, DESCRICAO_SERVICE) VALUES (?,?,?,?) returning ID_PESSOA into ?; END;";

        Connection con = factory.getConnection();
        CallableStatement cs = null;

        try {

            cs = con.prepareCall(sql);
            cs.setString(1, service.getNome);
            cs.setString(2, service.getTipo);
            cs.setString(3, service.getDescricao);

            cs.registerOutParameter(4, Types.BIGINT);

            cs.executeUpdate();

            service.setID(cs.getLong(5));
        }catch (SQLException e){
            System.err.println("Não foi possível inserir os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(null, cs, con);
        }
        return service;
    }
    private static  void  fecharObjetos(ResultSet rs, Statement st, Connection con){
        try {
            if (Objects.nonNull(rs) && !rs.isClosed()){
                rs.close();
            }
            st.close();
            con.close();
        }catch (SQLException e){
            System.err.println("Erro ao encerrar o ResultSet, a Connection e o Statment!\n" + e.getMessage());
        }

    }
}
