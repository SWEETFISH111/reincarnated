package com.github.sweetfish111.reincarnated.skill;

import com.github.sweetfish111.reincarnated.commondata.AbstractSkillHolder;

public interface ISkillAbility {
    /** 所持した瞬間に一度だけ呼ばれる(取得時の一回処理があれば) */
    default void onAcquire(AbstractSkillHolder holder) {
        holder.addOwnedSkillEffect(getAssociatedAbility());
    }

    /** 有効化された瞬間に一度だけ呼ばれる(ボックス投入時) */
    default void onActivate(AbstractSkillHolder holder) {
        holder.activateSkillEffect(getAssociatedAbility());
    }

    /** 無効化された瞬間に一度だけ呼ばれる(ボックスから外れた時) */
    default void onDeactivate(AbstractSkillHolder holder) {
        holder.deactivateSkillEffect(getAssociatedAbility());
    }

    //紐づいた能力を取得する
    SkillEffect getAssociatedAbility();

}
