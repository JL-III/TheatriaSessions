# **TheatriaSessions**

The **TheatriaSessions** plugin introduces a new way to reward players for their time and engagement on the server. This system replaces the traditional voting rewards with a focus on **active playtime**, ensuring rewards go to those who actively contribute to the community.

---

## **Why Replace Voting?**
The traditional voting system helped boost server visibility by encouraging players to vote on external sites. However, this often resulted in lower-quality engagements, with many players voting for rewards without spending meaningful time on the server.

**TheatriaSessions** shifts the focus back to rewarding active players, creating a more engaging and community-focused experience. By replacing voting with playtime-based rewards, we’re fostering a server environment that values participation and collaboration over fleeting visibility.

---

## **How It Works**
- Players can earn rewards by accumulating **active playtime** (currently set to 1 hour).
- Upon reaching 1 hour of active playtime, players are awarded **5 SessionCrate keys**.
- **Progress resets daily** at **0:00 UTC**:
    - Players who are online and actively working towards their reward during the reset will retain their progress.
    - Players who are offline during the reset will have their progress cleared.

---

## **Key Features**
1. **Daily Rewards**:
    - Earn rewards once per reset period (24 hours).
    - Rewards cannot be earned multiple times within the same reset period.

2. **AFK Tracking**:
    - AFK time does not count towards active playtime.
    - Players must remain actively engaged to earn progress.

3. **Fairness**:
    - Progress carries over through the reset if you’re online and actively working towards your reward.

---

## **Reward Details**
- **Active Playtime Required**: 1 hour
- **Reward**: 5 SessionCrate keys
- **Reset Period**: Daily at 0:00 UTC

---

### **Important Note on Voting**
The **voting system will be discontinued** as part of this transition to TheatriaSessions. While voting was useful for increasing visibility, we believe this new system will better reward those who actively contribute to the server’s community. Thank you for your understanding and for helping us create a more engaging experience for everyone.

---

### **Notes**
- Rewards are tracked automatically—no need to worry about manual claims.
- Ensure you’re actively playing and not AFK to make the most of your time.

---

### **Final Thoughts**
TheatriaSessions is our way of saying thank you for being part of the community. We’re excited to move forward with this new system and can’t wait to see you online earning your rewards!

---

## **Developer API**

Other plugins can ask TheatriaSessions whether a player has earned their daily
reward instead of inferring it from vanilla state. The supported surface is
`com.playtheatria.sessions.api.SessionsAPI`, obtained via the static
`SessionsAPI.get()` (returns `null` while the plugin is disabled). Every method
takes/returns only JDK types, so it can be consumed reflectively with no
compile-time dependency:

```java
Class<?> apiClass = Class.forName("com.playtheatria.sessions.api.SessionsAPI");
Object api = apiClass.getMethod("get").invoke(null);
if (api != null) {
    boolean earned = (boolean) apiClass
            .getMethod("hasEarnedDailyReward", java.util.UUID.class)
            .invoke(api, playerUuid);
}
```

| Method | Returns |
|--------|---------|
| `hasEarnedDailyReward(UUID)` | reward actually granted today (resets daily) |
| `hasMetThreshold(UUID)` | active playtime has reached the threshold |
| `getSessionSeconds(UUID)` | accumulated active (non-AFK) seconds today |
| `getThresholdSeconds()` | seconds of active playtime needed to earn |
| `hasSession(UUID)` | whether the player has a tracked session today |

A player only has a session while online and holding `theatria.sessions.allow`
(granted by default; removed only to exclude alts), so the query methods return
`false`/`0` for anyone without a current session.
