package br.com.fiap.domain.repository;

import br.com.fiap.domain.entity.pessoa.PJ;
import br.com.fiap.infra.ConnectionFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static br.com.fiap.domain.repository.PFRepository.fecharObjetos;

public class PJRepository implements Repository<PJ, Long>{
    private ConnectionFactory factory;
    private static final AtomicReference<PJRepository> instance = new AtomicReference<>();
    private PJRepository(){
        this.factory = ConnectionFactory.build();
    }
    public static PJRepository build() {
        instance.compareAndSet(null, new PJRepository());
        return instance.get();
    }
    @Override
    public List<PJ> findAll() {
        List<PJ> list = new ArrayList<>();
        Connection con = factory.getConnection();
        ResultSet rs = null;
        Statement st = null;
        try {
            String sql = "SELECT * FROM TB_PJ";
            st = con.createStatement();
            rs = st.executeQuery(sql);
            if (rs.isBeforeFirst()){
                while (rs.next()){
                Long id = rs.getLong("ID_PESSOA");
                String nome = rs.getString("NM_PESSOA");
                String tipo = rs.getString("TIPO_PESSOA");
                list.add(new PJ(id, nome, tipo));
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
    public PJ findById(Long id) {
        PJ pessoa = null;
        var sql = "SELECT * FROM TB_PJ where ID_PESSOA = ?";
        Connection con = factory.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.isBeforeFirst()){
                while (rs.next()){
                    String nome = rs.getString("NM_PESSOA");
                    LocalDate nascimento = rs.getDate("DT_NASCIMENTO").toLocalDate();
                    String cpf = rs.getString("NUMERO_CPF");
                    pessoa = new PJ(id, nome, nascimento, cpf);
                }
            }else {
                System.out.println("Dados não encontrados com o id: " + id);
            }
        }catch (SQLException e){
            System.err.println("Não foi possível consultar os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(rs, ps, con);
        }
        return pessoa;
    }

    @Override
    public PJ persiste(PJ pj) {

        var sql = "BEGIN INSERT INTO TB_PF (NM_PESSOA , DT_NASCIMENTO, TP_PESSOA, NR_CPF) VALUES (?,?,?,?) returning ID_PESSOA into ?; END;";

        Connection con = factory.getConnection();
        CallableStatement cs = null;
        try {

            cs = con.prepareCall(sql);
            cs.setString(1, pj.getCNPJ());
            cs.setString(2, pj.getTipo());
            cs.setString(3, pj.getNome());
            cs.setDate(4, Date.valueOf(pj.getNascimento()));
            cs.registerOutParameter(5, Types.BIGINT);
            cs.executeUpdate();
            pj.setId(cs.getLong(5));
        }catch (SQLException e){
            System.err.println("\"Não foi possível inserir os dados!\n" + e.getMessage());
        }finally {
            fecharObjetos(null, cs, con);
        }
        return pj;
    }
    private static void fecharObjetos(ResultSet rs, Statement st, Connection con){
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
