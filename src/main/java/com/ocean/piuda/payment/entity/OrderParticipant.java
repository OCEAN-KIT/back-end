package com.ocean.piuda.payment.entity;


import com.ocean.piuda.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor @Builder
@Table(name = "order_participant")
public class OrderParticipant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** 스냅샷: 결제 시점 이름 기록 */
    @Column(name = "participant_name_snapshot", length = 100, nullable = false)
    private String participantNameSnapshot;

    public void updateOrder(Order order){
        this.order = order;
    }
}
