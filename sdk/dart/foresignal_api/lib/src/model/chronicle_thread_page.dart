//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_thread_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_thread_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleThreadPage {
  /// Returns a new [ChronicleThreadPage] instance.
  ChronicleThreadPage({

    required  this.items,

    required  this.nextCursor,

     this.unreadCount,

     this.readUpTo,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleThreadItem> items;



      /// Opaque cursor for the next page; null at the end
  @JsonKey(
    
    name: r'nextCursor',
    required: true,
    includeIfNull: true,
  )


  final String? nextCursor;



  @JsonKey(
    
    name: r'unreadCount',
    required: false,
    includeIfNull: false,
  )


  final int? unreadCount;



      /// Caller's read cursor
  @JsonKey(
    
    name: r'readUpTo',
    required: false,
    includeIfNull: false,
  )


  final int? readUpTo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleThreadPage &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.unreadCount == unreadCount &&
      other.readUpTo == readUpTo;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        unreadCount.hashCode +
        (readUpTo == null ? 0 : readUpTo.hashCode);

  factory ChronicleThreadPage.fromJson(Map<String, dynamic> json) => _$ChronicleThreadPageFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleThreadPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

