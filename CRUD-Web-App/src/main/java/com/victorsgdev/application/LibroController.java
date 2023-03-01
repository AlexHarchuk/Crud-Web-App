package com.victorsgdev.application;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.victorsgdev.domain.Libro;
import com.victorsgdev.service.LibroService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Controller
//@RequestMapping()
public class LibroController {

    private final LibroService libroService;
    private final Libro libro;
    private String table = "Book";

    //DI BookService
    public LibroController(LibroService libroService, Libro libro) {
        this.libroService = libroService;
        this.libro = libro;
    }

    @GetMapping("/Book")
    public String listAll(Model model) {
        List<Libro> listBook = new ArrayList<>();
        var listObject = libroService.listAll(table);
        var rs = listObject.stream().map(List.class::cast);
        rs.forEach(list -> list.forEach(o -> listBook.add((Libro) o)));

        model.addAttribute("Book",
                listBook);
        return "book";
    }

    @GetMapping("/book/{id}")
    public Object listById(@PathVariable("id") Integer idBook) {
        return libroService.listById(idBook);
    }

    @GetMapping("/book/create")
    public String newBook(Model model){
        model.addAttribute("book", libro);
        return "create_book";
    }

    @PostMapping("/book")
    public String save(@ModelAttribute("book") Libro libro) {
        libroService.save(libro);
        return "redirect:/book";
    }

    @GetMapping("/book/edit/{id}")
    public String goToEditBook(@PathVariable Integer id, Model modelo) {
        var book = (Libro) libroService.listById(id);
        modelo.addAttribute("book",  libro);
        return "book_update";
    }



    @PostMapping("/book/{id}")
    public String updateBook(@PathVariable Integer id, @ModelAttribute("book") Libro libro) {
        libroService.updateById(id,libro);
        return "redirect:/book";
    }


    @GetMapping("/book/delete/{id}")
    public String deleteBook(@PathVariable Integer id) {
        libroService.deleteById(id);
        return "redirect:/book";
    }

    @GetMapping("/book/to-csv")
    public void getAllLibrosInCsv(HttpServletResponse servletResponse) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {

        String filename = "book.csv";

        servletResponse.setContentType("text/csv");
        servletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"");


        StatefulBeanToCsv<Libro> writer = new StatefulBeanToCsvBuilder<Libro>(servletResponse.getWriter())
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withOrderedResults(false)
                .build();

        List<Libro> listLibros = new ArrayList<>();
        var listObject = libroService.listAll(table);
        var rs = listObject.stream().map(List.class::cast);
        rs.forEach(list -> list.forEach(o -> listLibros.add((Libro) o)));

        writer.write(listLibros);
    }

    @PostMapping("/book/upload-csv-file")
    public String uploadCSVFile(@RequestParam("file") MultipartFile file) {

            try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

                CsvToBean<Libro> csvToBean = new CsvToBeanBuilder(reader)
                        .withType(Libro.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<Libro> libros = csvToBean.parse();

                libros.forEach(libroService::save);
                return "redirect:/book";


            } catch (Exception ex) {
                System.out.println("Не вдалося імпортувати");
            }

        return "book";
    }


}
