//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_split_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeSplitInput {
  /// Returns a new [NarrativeSplitInput] instance.
  NarrativeSplitInput({

    required  this.postRefs,

     this.reason,
  });

      /// Record refs of member posts
  @JsonKey(
    
    name: r'postRefs',
    required: true,
    includeIfNull: false,
  )


  final List<String> postRefs;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeSplitInput &&
      other.postRefs == postRefs &&
      other.reason == reason;

    @override
    int get hashCode =>
        postRefs.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory NarrativeSplitInput.fromJson(Map<String, dynamic> json) => _$NarrativeSplitInputFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeSplitInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

