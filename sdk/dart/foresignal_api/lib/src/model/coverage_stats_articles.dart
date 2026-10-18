//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'coverage_stats_articles.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CoverageStatsArticles {
  /// Returns a new [CoverageStatsArticles] instance.
  CoverageStatsArticles({

    required  this.left,

    required  this.center,

    required  this.right,

    required  this.unrated,
  });

  @JsonKey(
    
    name: r'left',
    required: true,
    includeIfNull: false,
  )


  final int left;



  @JsonKey(
    
    name: r'center',
    required: true,
    includeIfNull: false,
  )


  final int center;



  @JsonKey(
    
    name: r'right',
    required: true,
    includeIfNull: false,
  )


  final int right;



  @JsonKey(
    
    name: r'unrated',
    required: true,
    includeIfNull: false,
  )


  final int unrated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CoverageStatsArticles &&
      other.left == left &&
      other.center == center &&
      other.right == right &&
      other.unrated == unrated;

    @override
    int get hashCode =>
        left.hashCode +
        center.hashCode +
        right.hashCode +
        unrated.hashCode;

  factory CoverageStatsArticles.fromJson(Map<String, dynamic> json) => _$CoverageStatsArticlesFromJson(json);

  Map<String, dynamic> toJson() => _$CoverageStatsArticlesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

