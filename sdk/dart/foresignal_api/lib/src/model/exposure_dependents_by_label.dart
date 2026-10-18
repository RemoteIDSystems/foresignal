//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_dependents_by_label.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureDependentsByLabel {
  /// Returns a new [ExposureDependentsByLabel] instance.
  ExposureDependentsByLabel({

     this.confirmed,

     this.likely,

     this.possible,

     this.speculative,
  });

  @JsonKey(
    
    name: r'confirmed',
    required: false,
    includeIfNull: false,
  )


  final int? confirmed;



  @JsonKey(
    
    name: r'likely',
    required: false,
    includeIfNull: false,
  )


  final int? likely;



  @JsonKey(
    
    name: r'possible',
    required: false,
    includeIfNull: false,
  )


  final int? possible;



  @JsonKey(
    
    name: r'speculative',
    required: false,
    includeIfNull: false,
  )


  final int? speculative;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureDependentsByLabel &&
      other.confirmed == confirmed &&
      other.likely == likely &&
      other.possible == possible &&
      other.speculative == speculative;

    @override
    int get hashCode =>
        confirmed.hashCode +
        likely.hashCode +
        possible.hashCode +
        speculative.hashCode;

  factory ExposureDependentsByLabel.fromJson(Map<String, dynamic> json) => _$ExposureDependentsByLabelFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureDependentsByLabelToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

