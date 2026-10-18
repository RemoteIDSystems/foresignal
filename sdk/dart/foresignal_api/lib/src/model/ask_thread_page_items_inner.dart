//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_thread_page_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskThreadPageItemsInner {
  /// Returns a new [AskThreadPageItemsInner] instance.
  AskThreadPageItemsInner({

     this.id,

     this.title,

     this.createdAt,

     this.updatedAt,

     this.messageCount,
  });

  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'messageCount',
    required: false,
    includeIfNull: false,
  )


  final int? messageCount;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskThreadPageItemsInner &&
      other.id == id &&
      other.title == title &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.messageCount == messageCount;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        messageCount.hashCode;

  factory AskThreadPageItemsInner.fromJson(Map<String, dynamic> json) => _$AskThreadPageItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AskThreadPageItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

