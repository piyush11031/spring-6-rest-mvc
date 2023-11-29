package guru.springframework.spring6restmvc.entities;

import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Beer {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    private UUID id;

    @Version
    private Integer version;

    @Size(max = 50)
    @Column(length = 50)
    @NotBlank
    @NotNull
    private String beerName;

    @NotNull
    private BeerStyle beerStyle;

    @Size(max = 255)
    @NotNull
    private String upc;

    private Integer quantityOnHand;

    @NotNull
    private BigDecimal price;

    @CreationTimestamp //hibernate annotation
    private LocalDateTime createdDate;

    @UpdateTimestamp //hibernate annotation
    private LocalDateTime updateDate;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "beer_category", //name of Join table
        joinColumns = @JoinColumn(name = "beer_id"), //Id of current entity
        inverseJoinColumns = @JoinColumn(name = "category_id")) //Id of related entity
    private Set<Category> categories = new HashSet<>();

    //We want to add and remove category from the beer object
    public void addCategory(Category category){
        this.categories.add(category);
        category.getBeers().add(this);
    }

    public void removeCategory(Category category){
        this.categories.remove(category);
        category.getBeers().remove(category);
    }


    @OneToMany(mappedBy = "beer")
    private Set<BeerOrderLine> beerOrderLines;
}