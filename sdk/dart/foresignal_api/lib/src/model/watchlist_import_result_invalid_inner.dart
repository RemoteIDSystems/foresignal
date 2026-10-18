//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchlist_import_result_invalid_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchlistImportResultInvalidInner {
  /// Returns a new [WatchlistImportResultInvalidInner] instance.
  WatchlistImportResultInvalidInner({

     this.row,

     this.value,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'row',
    required: false,
    includeIfNull: false,
  )


  final int? row;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final String? value;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchlistImportResultInvalidInner &&
      other.row == row &&
      other.value == value &&
      other.reason == reason;

    @override
    int get hashCode =>
        (row == null ? 0 : row.hashCode) +
        (value == null ? 0 : value.hashCode) +
        reason.hashCode;

  factory WatchlistImportResultInvalidInner.fromJson(Map<String, dynamic> json) => _$WatchlistImportResultInvalidInnerFromJson(json);

  Map<String, dynamic> toJson() => _$WatchlistImportResultInvalidInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

