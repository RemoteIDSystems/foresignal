//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchlist_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchlistItem {
  /// Returns a new [WatchlistItem] instance.
  WatchlistItem({

    required  this.value,

     this.label,

     this.notes,

     this.tags,

    required  this.addedAt,

    required  this.origin,
  });

      /// Normalised
  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final String value;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'addedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime addedAt;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final WatchlistItemOriginEnum origin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchlistItem &&
      other.value == value &&
      other.label == label &&
      other.notes == notes &&
      other.tags == tags &&
      other.addedAt == addedAt &&
      other.origin == origin;

    @override
    int get hashCode =>
        value.hashCode +
        (label == null ? 0 : label.hashCode) +
        (notes == null ? 0 : notes.hashCode) +
        tags.hashCode +
        addedAt.hashCode +
        origin.hashCode;

  factory WatchlistItem.fromJson(Map<String, dynamic> json) => _$WatchlistItemFromJson(json);

  Map<String, dynamic> toJson() => _$WatchlistItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WatchlistItemOriginEnum {
@JsonValue(r'manual')
manual(r'manual'),
@JsonValue(r'csv')
csv(r'csv'),
@JsonValue(r'json')
json(r'json');

const WatchlistItemOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


