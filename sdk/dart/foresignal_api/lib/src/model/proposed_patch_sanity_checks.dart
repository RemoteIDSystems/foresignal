//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'proposed_patch_sanity_checks.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProposedPatchSanityChecks {
  /// Returns a new [ProposedPatchSanityChecks] instance.
  ProposedPatchSanityChecks({

     this.bracesBalanced,

     this.classNameUnchanged,
  });

  @JsonKey(
    
    name: r'bracesBalanced',
    required: false,
    includeIfNull: false,
  )


  final bool? bracesBalanced;



  @JsonKey(
    
    name: r'classNameUnchanged',
    required: false,
    includeIfNull: false,
  )


  final bool? classNameUnchanged;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProposedPatchSanityChecks &&
      other.bracesBalanced == bracesBalanced &&
      other.classNameUnchanged == classNameUnchanged;

    @override
    int get hashCode =>
        bracesBalanced.hashCode +
        classNameUnchanged.hashCode;

  factory ProposedPatchSanityChecks.fromJson(Map<String, dynamic> json) => _$ProposedPatchSanityChecksFromJson(json);

  Map<String, dynamic> toJson() => _$ProposedPatchSanityChecksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

