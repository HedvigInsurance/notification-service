package com.hedvig.notificationService.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
public class FirebaseToken {

    @Id
    public String memberId;

    @NotNull
    public String token;

    public FirebaseToken() {
    }

    public String getMemberId() {
        return this.memberId;
    }

    public @NotNull String getToken() {
        return this.token;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setToken(@NotNull String token) {
        this.token = token;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FirebaseToken)) return false;
        final FirebaseToken other = (FirebaseToken) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$memberId = this.getMemberId();
        final Object other$memberId = other.getMemberId();
        if (!Objects.equals(this$memberId, other$memberId)) return false;
        final Object this$token = this.getToken();
        final Object other$token = other.getToken();
        if (!Objects.equals(this$token, other$token)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FirebaseToken;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $memberId = this.getMemberId();
        result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
        final Object $token = this.getToken();
        result = result * PRIME + ($token == null ? 43 : $token.hashCode());
        return result;
    }

    public String toString() {
        return "FirebaseToken(memberId=" + this.getMemberId() + ", token=" + this.getToken() + ")";
    }
}
