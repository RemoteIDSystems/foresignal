//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/proposed_patch_sanity_checks.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'proposed_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProposedPatch {
  /// Returns a new [ProposedPatch] instance.
  ProposedPatch({

    required  this.file,

    required  this.unifiedDiff,

    required  this.patchApplies,

     this.sanityChecks,

    required  this.label,

     this.error,
  });

  @JsonKey(
    
    name: r'file',
    required: true,
    includeIfNull: false,
  )


  final String file;



  @JsonKey(
    
    name: r'unifiedDiff',
    required: true,
    includeIfNull: false,
  )


  final String unifiedDiff;



  @JsonKey(
    
    name: r'patchApplies',
    required: true,
    includeIfNull: false,
  )


  final bool patchApplies;



  @JsonKey(
    
    name: r'sanityChecks',
    required: false,
    includeIfNull: false,
  )


  final ProposedPatchSanityChecks? sanityChecks;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final ProposedPatchLabelEnum label;



      /// Why the patch does not apply, or why its file was refused (not in the adapter source catalog)
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProposedPatch &&
      other.file == file &&
      other.unifiedDiff == unifiedDiff &&
      other.patchApplies == patchApplies &&
      other.sanityChecks == sanityChecks &&
      other.label == label &&
      other.error == error;

    @override
    int get hashCode =>
        file.hashCode +
        unifiedDiff.hashCode +
        patchApplies.hashCode +
        sanityChecks.hashCode +
        label.hashCode +
        (error == null ? 0 : error.hashCode);

  factory ProposedPatch.fromJson(Map<String, dynamic> json) => _$ProposedPatchFromJson(json);

  Map<String, dynamic> toJson() => _$ProposedPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProposedPatchLabelEnum {
@JsonValue(r'proposed, not applied')
proposedCommaNotApplied(r'proposed, not applied');

const ProposedPatchLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


