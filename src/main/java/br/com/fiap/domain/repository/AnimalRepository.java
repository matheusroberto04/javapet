package br.com.fiap.domain.repository;

import br.com.fiap.domain.entity.animal.Animal;
import br.com.fiap.infra.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static br.com.fiap.domain.repository.PFRepository.fecharObjetos;

public class AnimalRepository implements Repository<Animal, Long> {
    private ConnectionFactory factory;
    private static final AtomicReference<AnimalRepository> instance = new AtomicReference<>();
    private AnimalRepository(){
        this.factory = ConnectionFactory.build();
    }
    public static AnimalRepository build(){
        instance.compareAndSet(null, new AnimalRepository());
        return instance.get();
    }
    @Override
    public List<Animal> findAll() {
        List<Animal> list = new ArrayList<>();
        Connection con = factory.getConnection();
        ResultSet rs = null;
        Statement st = null;
        try {
            String sql = "SELECT * FROM TB_ANIMAL";
            st = con.createStatement();
            rs = st.executeQuery(sql);
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    Long id = rs.getLong("ID_ANIMAL");
                    String nome = rs.getString("NM_ANIMAL");
                    String raca = rs.getString("RACA_ANIMAL");
                    String descricao = rs.getString("DESCRICAO_ANIMAL");
                    String dono = rs.getString("TIPO_ANIMAL");
                    list.add(new Animal(nome, raca) {
                    });
                }
            }
        }catch (SQLException e){
            System.err.println("Não foi possível consultar os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(rs, st, con);
        }
        return list;
    }

    @Override
    public Animal findById(Long id) {
        Animal dono = null;
        var sql = "SELECT * FROM TB_ANIMAL where ID_ANIMAL = ?";
        Connection con = factory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    String nome = rs.getString("NM_ANIMAL");
                    String raca = rs.getString("RACA_ANIMAL");
                    dono = new Animal(nome, raca);
                }
            }else {
                System.out.println("Dados nao encontrados com o id: " + id);
            }
        }catch (SQLException e) {
            System.err.println("Não foi possível consultar os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(rs , ps, con);
        }
        return dono;
    }
    @Override
    public Animal persiste(Animal animal) {
        var sql ="BEGIN INSERT INTO TB_ANIMAL(NM_ANIMAL, RACA_ANIMAL, DONO_ANIMAL, DESCRICAO_ANIMAL) VALUES (?,?,?,?) returning ID_ANIMAL into ?; END;";

        Connection con = factory.getConnection();
        CallableStatement cs = null;
        try {
            cs = con.prepareCall(sql);
            cs.setString(1, animal.getNome());
            cs.setString(2, animal.getRaca());
            cs.setString(3, animal.getDescricao());
            cs.setString(4, animal.getTipo());

            cs.registerOutParameter(5, Types.BIGINT);

            cs.executeUpdate();

            animal.setId(cs.getLong(5));
        }catch (SQLException e){
            System.err.println("Não foi possível inserir os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(null, cs, con);
        }
        return animal;
    }
    static void fecharObjetos(ResultSet rs, Statement st, Connection con){
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
