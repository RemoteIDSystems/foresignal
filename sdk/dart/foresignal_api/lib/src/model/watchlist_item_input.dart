//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchlist_item_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchlistItemInput {
  /// Returns a new [WatchlistItemInput] instance.
  WatchlistItemInput({

    required  this.value,

     this.label,

     this.notes,

     this.tags,
  });

      /// Validated per identity (icao24 6 hex; mmsi 9 digits; imo 7 digits with check digit) and normalised
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchlistItemInput &&
      other.value == value &&
      other.label == label &&
      other.notes == notes &&
      other.tags == tags;

    @override
    int get hashCode =>
        value.hashCode +
        (label == null ? 0 : label.hashCode) +
        (notes == null ? 0 : notes.hashCode) +
        tags.hashCode;

  factory WatchlistItemInput.fromJson(Map<String, dynamic> json) => _$WatchlistItemInputFromJson(json);

  Map<String, dynamic> toJson() => _$WatchlistItemInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

