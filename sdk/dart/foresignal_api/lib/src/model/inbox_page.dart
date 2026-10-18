//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/inbox_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'inbox_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InboxPage {
  /// Returns a new [InboxPage] instance.
  InboxPage({

    required  this.items,

    required  this.nextCursor,

     this.unread,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<InboxItem> items;



      /// Opaque cursor for the next page; null at the end
  @JsonKey(
    
    name: r'nextCursor',
    required: true,
    includeIfNull: true,
  )


  final String? nextCursor;



      /// Unread items for the caller
  @JsonKey(
    
    name: r'unread',
    required: false,
    includeIfNull: false,
  )


  final int? unread;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InboxPage &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.unread == unread;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        unread.hashCode;

  factory InboxPage.fromJson(Map<String, dynamic> json) => _$InboxPageFromJson(json);

  Map<String, dynamic> toJson() => _$InboxPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

