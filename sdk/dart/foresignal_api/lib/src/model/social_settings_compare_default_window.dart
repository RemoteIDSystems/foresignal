//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_compare_default_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsCompareDefaultWindow {
  /// Returns a new [SocialSettingsCompareDefaultWindow] instance.
  SocialSettingsCompareDefaultWindow({

     this.before,

     this.after,
  });

      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'before',
    required: false,
    includeIfNull: false,
  )


  final String? before;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'after',
    required: false,
    includeIfNull: false,
  )


  final String? after;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsCompareDefaultWindow &&
      other.before == before &&
      other.after == after;

    @override
    int get hashCode =>
        before.hashCode +
        after.hashCode;

  factory SocialSettingsCompareDefaultWindow.fromJson(Map<String, dynamic> json) => _$SocialSettingsCompareDefaultWindowFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsCompareDefaultWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

