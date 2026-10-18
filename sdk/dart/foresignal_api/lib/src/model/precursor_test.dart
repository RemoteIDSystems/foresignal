//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_test_antecedent.dart';
import 'package:foresignal_api/src/model/precursor_test_spatial.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTest {
  /// Returns a new [PrecursorTest] instance.
  PrecursorTest({

    required  this.id,

    required  this.name,

     this.statement,

    required  this.antecedent,

    required  this.consequent,

     this.minLag,

    required  this.maxLag,

    required  this.spatial,

     this.dedupWindow,

     this.createdBy,

    required  this.createdAt,

     this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Plain-language statement
  @JsonKey(
    
    name: r'statement',
    required: false,
    includeIfNull: false,
  )


  final String? statement;



  @JsonKey(
    
    name: r'antecedent',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestAntecedent antecedent;



  @JsonKey(
    
    name: r'consequent',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestAntecedent consequent;



      /// Minimum lag (default 0). ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'minLag',
    required: false,
    includeIfNull: false,
  )


  final String? minLag;



      /// Maximum lag, ≤ 30d. ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'maxLag',
    required: true,
    includeIfNull: false,
  )


  final String maxLag;



  @JsonKey(
    
    name: r'spatial',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestSpatial spatial;



      /// Antecedent occurrences closer than this merge (default maxLag). ISO-8601 duration or shorthand (30m, 6h, 7d).
  @JsonKey(
    
    name: r'dedupWindow',
    required: false,
    includeIfNull: false,
  )


  final String? dedupWindow;



  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTest &&
      other.id == id &&
      other.name == name &&
      other.statement == statement &&
      other.antecedent == antecedent &&
      other.consequent == consequent &&
      other.minLag == minLag &&
      other.maxLag == maxLag &&
      other.spatial == spatial &&
      other.dedupWindow == dedupWindow &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        statement.hashCode +
        antecedent.hashCode +
        consequent.hashCode +
        minLag.hashCode +
        maxLag.hashCode +
        spatial.hashCode +
        dedupWindow.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory PrecursorTest.fromJson(Map<String, dynamic> json) => _$PrecursorTestFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

