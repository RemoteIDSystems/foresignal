//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_benchmarks_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardBenchmarksInner {
  /// Returns a new [IntelBoardBenchmarksInner] instance.
  IntelBoardBenchmarksInner({

    required  this.component,

    required  this.status,
  });

  @JsonKey(
    
    name: r'component',
    required: true,
    includeIfNull: false,
  )


  final String component;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final IntelBoardBenchmarksInnerStatusEnum status;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardBenchmarksInner &&
      other.component == component &&
      other.status == status;

    @override
    int get hashCode =>
        component.hashCode +
        status.hashCode;

  factory IntelBoardBenchmarksInner.fromJson(Map<String, dynamic> json) => _$IntelBoardBenchmarksInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardBenchmarksInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelBoardBenchmarksInnerStatusEnum {
@JsonValue(r'pass')
pass(r'pass'),
@JsonValue(r'warn')
warn(r'warn'),
@JsonValue(r'fail')
fail(r'fail'),
@JsonValue(r'unknown')
unknown(r'unknown');

const IntelBoardBenchmarksInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


