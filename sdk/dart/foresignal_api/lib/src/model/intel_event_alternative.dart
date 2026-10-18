//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_alternative.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventAlternative {
  /// Returns a new [IntelEventAlternative] instance.
  IntelEventAlternative({

    required  this.hypothesis,

    required  this.basis,

     this.supportingMemberIds,
  });

  @JsonKey(
    
    name: r'hypothesis',
    required: true,
    includeIfNull: false,
  )


  final String hypothesis;



  @JsonKey(
    
    name: r'basis',
    required: true,
    includeIfNull: false,
  )


  final String basis;



  @JsonKey(
    
    name: r'supportingMemberIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? supportingMemberIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventAlternative &&
      other.hypothesis == hypothesis &&
      other.basis == basis &&
      other.supportingMemberIds == supportingMemberIds;

    @override
    int get hashCode =>
        hypothesis.hashCode +
        basis.hashCode +
        supportingMemberIds.hashCode;

  factory IntelEventAlternative.fromJson(Map<String, dynamic> json) => _$IntelEventAlternativeFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventAlternativeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

