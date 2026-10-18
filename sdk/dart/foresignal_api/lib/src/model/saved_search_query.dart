//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/saved_search_query_area.dart';
import 'package:foresignal_api/src/model/saved_search_query_time_window.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_query.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchQuery {
  /// Returns a new [SavedSearchQuery] instance.
  SavedSearchQuery({

     this.types,

     this.text,

     this.filters,

     this.area,

     this.timeWindow,

     this.tags,
  });

      /// Object types (Client-Contract §2); empty = all
  @JsonKey(
    
    name: r'types',
    required: false,
    includeIfNull: false,
  )


  final List<String>? types;



  @JsonKey(
    
    name: r'text',
    required: false,
    includeIfNull: false,
  )


  final String? text;



      /// Structured filters (sources, kinds, severity, status…)
  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? filters;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final SavedSearchQueryArea? area;



  @JsonKey(
    
    name: r'timeWindow',
    required: false,
    includeIfNull: false,
  )


  final SavedSearchQueryTimeWindow? timeWindow;



      /// Tag ids the objects must carry
  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchQuery &&
      other.types == types &&
      other.text == text &&
      other.filters == filters &&
      other.area == area &&
      other.timeWindow == timeWindow &&
      other.tags == tags;

    @override
    int get hashCode =>
        types.hashCode +
        (text == null ? 0 : text.hashCode) +
        filters.hashCode +
        (area == null ? 0 : area.hashCode) +
        (timeWindow == null ? 0 : timeWindow.hashCode) +
        tags.hashCode;

  factory SavedSearchQuery.fromJson(Map<String, dynamic> json) => _$SavedSearchQueryFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchQueryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

