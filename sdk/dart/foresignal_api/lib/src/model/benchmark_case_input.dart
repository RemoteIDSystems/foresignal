//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_case_expectations.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_case_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkCaseInput {
  /// Returns a new [BenchmarkCaseInput] instance.
  BenchmarkCaseInput({

    required  this.name,

     this.description,

    required  this.windowFrom,

    required  this.windowTo,

     this.regionCodes,

    required  this.expectations,
  });

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
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'expectations',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkCaseExpectations expectations;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkCaseInput &&
      other.name == name &&
      other.description == description &&
      other.windowFrom == windowFrom &&
      other.windowTo == windowTo &&
      other.regionCodes == regionCodes &&
      other.expectations == expectations;

    @override
    int get hashCode =>
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        windowFrom.hashCode +
        windowTo.hashCode +
        regionCodes.hashCode +
        expectations.hashCode;

  factory BenchmarkCaseInput.fromJson(Map<String, dynamic> json) => _$BenchmarkCaseInputFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkCaseInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

