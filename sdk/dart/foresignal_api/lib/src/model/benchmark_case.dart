//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_case_expectations.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_case.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkCase {
  /// Returns a new [BenchmarkCase] instance.
  BenchmarkCase({

    required  this.id,

    required  this.name,

     this.description,

    required  this.windowFrom,

    required  this.windowTo,

    required  this.regionCodes,

    required  this.expectations,

    required  this.source_,

     this.createdBy,

    required  this.createdAt,
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



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'windowFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime windowFrom;



  @JsonKey(
    
    name: r'windowTo',
    required: true,
    includeIfNull: false,
  )


  final DateTime windowTo;



      /// ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
  @JsonKey(
    
    name: r'regionCodes',
    required: true,
    includeIfNull: false,
  )


  final List<String> regionCodes;



  @JsonKey(
    
    name: r'expectations',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkCaseExpectations expectations;



      /// `auto` = sampled null region-week
  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkCaseSource_Enum source_;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkCase &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.windowFrom == windowFrom &&
      other.windowTo == windowTo &&
      other.regionCodes == regionCodes &&
      other.expectations == expectations &&
      other.source_ == source_ &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        windowFrom.hashCode +
        windowTo.hashCode +
        regionCodes.hashCode +
        expectations.hashCode +
        source_.hashCode +
        (createdBy == null ? 0 : createdBy.hashCode) +
        createdAt.hashCode;

  factory BenchmarkCase.fromJson(Map<String, dynamic> json) => _$BenchmarkCaseFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkCaseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `auto` = sampled null region-week
enum BenchmarkCaseSource_Enum {
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'auto')
auto(r'auto');

const BenchmarkCaseSource_Enum(this.value);

final String value;

@override
String toString() => value;
}


