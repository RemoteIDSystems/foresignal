//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_top_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsTopItemsInner {
  /// Returns a new [StatsTopItemsInner] instance.
  StatsTopItemsInner({

    required  this.key,

     this.sourceId,

     this.kind,

     this.cell,

     this.externalId,

     this.title,

    required  this.count,
  });

  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



      /// H3 index (hex string)
  @JsonKey(
    
    name: r'cell',
    required: false,
    includeIfNull: false,
  )


  final String? cell;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsTopItemsInner &&
      other.key == key &&
      other.sourceId == sourceId &&
      other.kind == kind &&
      other.cell == cell &&
      other.externalId == externalId &&
      other.title == title &&
      other.count == count;

    @override
    int get hashCode =>
        key.hashCode +
        (sourceId == null ? 0 : sourceId.hashCode) +
        (kind == null ? 0 : kind.hashCode) +
        (cell == null ? 0 : cell.hashCode) +
        (externalId == null ? 0 : externalId.hashCode) +
        (title == null ? 0 : title.hashCode) +
        count.hashCode;

  factory StatsTopItemsInner.fromJson(Map<String, dynamic> json) => _$StatsTopItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StatsTopItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

