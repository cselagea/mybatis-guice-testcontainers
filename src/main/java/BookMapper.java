import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BookMapper {

    @Select("select * from book")
    List<Book> selectBooks();

}
