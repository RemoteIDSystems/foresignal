//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_annul_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastAnnulInput {
  /// Returns a new [ForecastAnnulInput] instance.
  ForecastAnnulInput({

    required  this.reason,
  });

  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastAnnulInput &&
      other.reason == reason;

    @override
    int get hashCode =>
        reason.hashCode;

  factory ForecastAnnulInput.fromJson(Map<String, dynamic> json) => _$ForecastAnnulInputFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastAnnulInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

