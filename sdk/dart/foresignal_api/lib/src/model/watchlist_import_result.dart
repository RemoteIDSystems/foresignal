//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/watchlist_import_result_invalid_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchlist_import_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchlistImportResult {
  /// Returns a new [WatchlistImportResult] instance.
  WatchlistImportResult({

    required  this.dryRun,

     this.mode,

    required  this.added,

    required  this.updated,

    required  this.removed,

     this.unchanged,

    required  this.invalid,

    required  this.itemCount,
  });

  @JsonKey(
    
    name: r'dryRun',
    required: true,
    includeIfNull: false,
  )


  final bool dryRun;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final WatchlistImportResultModeEnum? mode;



  @JsonKey(
    
    name: r'added',
    required: true,
    includeIfNull: false,
  )


  final int added;



  @JsonKey(
    
    name: r'updated',
    required: true,
    includeIfNull: false,
  )


  final int updated;



  @JsonKey(
    
    name: r'removed',
    required: true,
    includeIfNull: false,
  )


  final int removed;



  @JsonKey(
    
    name: r'unchanged',
    required: false,
    includeIfNull: false,
  )


  final int? unchanged;



  @JsonKey(
    
    name: r'invalid',
    required: true,
    includeIfNull: false,
  )


  final List<WatchlistImportResultInvalidInner> invalid;



      /// Items after the operation (or as it would be)
  @JsonKey(
    
    name: r'itemCount',
    required: true,
    includeIfNull: false,
  )


  final int itemCount;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchlistImportResult &&
      other.dryRun == dryRun &&
      other.mode == mode &&
      other.added == added &&
      other.updated == updated &&
      other.removed == removed &&
      other.unchanged == unchanged &&
      other.invalid == invalid &&
      other.itemCount == itemCount;

    @override
    int get hashCode =>
        dryRun.hashCode +
        mode.hashCode +
        added.hashCode +
        updated.hashCode +
        removed.hashCode +
        unchanged.hashCode +
        invalid.hashCode +
        itemCount.hashCode;

  factory WatchlistImportResult.fromJson(Map<String, dynamic> json) => _$WatchlistImportResultFromJson(json);

  Map<String, dynamic> toJson() => _$WatchlistImportResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WatchlistImportResultModeEnum {
@JsonValue(r'merge')
merge(r'merge'),
@JsonValue(r'replace')
replace(r'replace');

const WatchlistImportResultModeEnum(this.value);

final String value;

@override
String toString() => value;
}


