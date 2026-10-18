//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog_result_signature.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalogResultSignature {
  /// Returns a new [AnalogResultSignature] instance.
  AnalogResultSignature({

    required  this.dayN,

     this.computedAt,
  });

  @JsonKey(
    
    name: r'dayN',
    required: true,
    includeIfNull: false,
  )


  final int dayN;



  @JsonKey(
    
    name: r'computedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalogResultSignature &&
      other.dayN == dayN &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        dayN.hashCode +
        computedAt.hashCode;

  factory AnalogResultSignature.fromJson(Map<String, dynamic> json) => _$AnalogResultSignatureFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogResultSignatureToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

