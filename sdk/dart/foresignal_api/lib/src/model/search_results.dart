//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/search_result.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'search_results.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SearchResults {
  /// Returns a new [SearchResults] instance.
  SearchResults({

    required  this.mode,

    required  this.items,

     this.degraded,

     this.attributions,

     this.licenceFiltered,

     this.reason,
  });

  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final SearchResultsModeEnum mode;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<SearchResult> items;



      /// True when semantic was requested but text was used
  @JsonKey(
    
    name: r'degraded',
    required: false,
    includeIfNull: false,
  )


  final bool? degraded;



  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;



      /// Why semantic results were not used, when `degraded` is true (027)
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SearchResults &&
      other.mode == mode &&
      other.items == items &&
      other.degraded == degraded &&
      other.attributions == attributions &&
      other.licenceFiltered == licenceFiltered &&
      other.reason == reason;

    @override
    int get hashCode =>
        mode.hashCode +
        items.hashCode +
        degraded.hashCode +
        attributions.hashCode +
        licenceFiltered.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory SearchResults.fromJson(Map<String, dynamic> json) => _$SearchResultsFromJson(json);

  Map<String, dynamic> toJson() => _$SearchResultsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SearchResultsModeEnum {
@JsonValue(r'hybrid')
hybrid(r'hybrid'),
@JsonValue(r'semantic')
semantic(r'semantic'),
@JsonValue(r'text')
text(r'text');

const SearchResultsModeEnum(this.value);

final String value;

@override
String toString() => value;
}


