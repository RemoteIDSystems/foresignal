//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_split_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationSplitInput {
  /// Returns a new [SituationSplitInput] instance.
  SituationSplitInput({

    required  this.memberIds,

    required  this.title,

     this.theme,

     this.version,
  });

  @JsonKey(
    
    name: r'memberIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> memberIds;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'theme',
    required: false,
    includeIfNull: false,
  )


  final SituationTheme? theme;



      /// Expected version of the source situation
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationSplitInput &&
      other.memberIds == memberIds &&
      other.title == title &&
      other.theme == theme &&
      other.version == version;

    @override
    int get hashCode =>
        memberIds.hashCode +
        title.hashCode +
        theme.hashCode +
        version.hashCode;

  factory SituationSplitInput.fromJson(Map<String, dynamic> json) => _$SituationSplitInputFromJson(json);

  Map<String, dynamic> toJson() => _$SituationSplitInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

