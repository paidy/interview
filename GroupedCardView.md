# A grouped card view.

Build a collection of grouped card views with shadow applied

__Requirements__

"A card is a sheet of material that serves as an entry point to more detailed information.
Cards may contain a photo, text, and a link about a single subject. 
They may display content containing elements of varying size, such as photos with captions of variable length.
A card collection is a layout of cards on the same plane."
[reference](https://material.io/guidelines/components/cards.html)

Building collection of grouped card view as shown in [mockup](https://drive.google.com/file/d/1jBneZDZp5UWkuD6xIXDLrSH26xBFVuWw).
- Cell size automatically resizes to fit content
- Section header is plain text in bold
- Section footer (if needed) is plain text
- Top card of the group has rounded corner on top left and top right
- Bottom card of the group has rounded corner on bottom left and bottom right
- Each card in the group can be tapped to do some action
- Cards with action (tappable) should have stronger shadow than cards without action (untappable).
- When you tap cards with action, the app presents an alert dialog with the id in the title, a description in the message, and an OK button for dismissing the dialog.
- Shadow should have same amount of rounded corner as the card itself (for top and bottom card)
- Shadow applied on the card should be consistent across the group. [example of inconsistent shadow](https://drive.google.com/file/d/1D08twXKldXZJPCHmXMwbANLiNfVPYWcC/preview)
- Use datasource from http://www.mocky.io/v2/5a275eb23000006e3c0e8a5e

Some key points to keep in mind when doing this:
- For iOS, implement using UITableView or UICollectionView
- For Android, implement using RecyclerView and CardView
- Should work on all screen sizes.
- Drawing a shadow on the cell will impact app performance when scrolling
- App should be able to handle the case where there is no internet connection (for example, presents an alert dialog with a message about no internet connection)
