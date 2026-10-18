//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_claim.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystClaim {
  /// Returns a new [AnalystClaim] instance.
  AnalystClaim({

    required  this.text,

    required  this.basis,

    required  this.citations,

     this.downgraded,

     this.warning,
  });

  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'basis',
    required: true,
    includeIfNull: false,
  )


  final AnalystClaimBasisEnum basis;



      /// Item ids returned by tools in this run: record@seq, alert, anomaly or event
  @JsonKey(
    
    name: r'citations',
    required: true,
    includeIfNull: false,
  )


  final List<String> citations;



  @JsonKey(
    
    name: r'downgraded',
    required: false,
    includeIfNull: false,
  )


  final bool? downgraded;



  @JsonKey(
    
    name: r'warning',
    required: false,
    includeIfNull: false,
  )


  final String? warning;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystClaim &&
      other.text == text &&
      other.basis == basis &&
      other.citations == citations &&
      other.downgraded == downgraded &&
      other.warning == warning;

    @override
    int get hashCode =>
        text.hashCode +
        basis.hashCode +
        citations.hashCode +
        downgraded.hashCode +
        (warning == null ? 0 : warning.hashCode);

  factory AnalystClaim.fromJson(Map<String, dynamic> json) => _$AnalystClaimFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystClaimToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystClaimBasisEnum {
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'inference')
inference(r'inference'),
@JsonValue(r'speculation')
speculation(r'speculation');

const AnalystClaimBasisEnum(this.value);

final String value;

@override
String toString() => value;
}


