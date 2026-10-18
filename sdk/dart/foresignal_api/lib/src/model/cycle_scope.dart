//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_scope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CycleScope {
  /// Returns a new [CycleScope] instance.
  CycleScope({

     this.regionCodes,

     this.themes,

     this.situationIds,

     this.hypothesisSetIds,

     this.sourceIds,

     this.categories,
  });

      /// ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'themes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? themes;



  @JsonKey(
    
    name: r'situationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? situationIds;



      /// Set ids, or `cadence-match` for every set whose cadence matches
  @JsonKey(
    
    name: r'hypothesisSetIds',
    required: false,
    includeIfNull: false,
  )


  final Object? hypothesisSetIds;



  @JsonKey(
    
    name: r'sourceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceIds;



  @JsonKey(
    
    name: r'categories',
    required: false,
    includeIfNull: false,
  )


  final List<String>? categories;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CycleScope &&
      other.regionCodes == regionCodes &&
      other.themes == themes &&
      other.situationIds == situationIds &&
      other.hypothesisSetIds == hypothesisSetIds &&
      other.sourceIds == sourceIds &&
      other.categories == categories;

    @override
    int get hashCode =>
        regionCodes.hashCode +
        themes.hashCode +
        situationIds.hashCode +
        (hypothesisSetIds == null ? 0 : hypothesisSetIds.hashCode) +
        sourceIds.hashCode +
        categories.hashCode;

  factory CycleScope.fromJson(Map<String, dynamic> json) => _$CycleScopeFromJson(json);

  Map<String, dynamic> toJson() => _$CycleScopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

