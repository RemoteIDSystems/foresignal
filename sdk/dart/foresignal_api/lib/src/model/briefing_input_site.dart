//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_input_site.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingInputSite {
  /// Returns a new [BriefingInputSite] instance.
  BriefingInputSite({

     this.enabled = false,

     this.shareable = false,
  });

      /// Serve the latest successful run at `getEditionSite`
  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Keep a read-only, revocable share link to the site
  @JsonKey(
    defaultValue: false,
    name: r'shareable',
    required: false,
    includeIfNull: false,
  )


  final bool? shareable;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingInputSite &&
      other.enabled == enabled &&
      other.shareable == shareable;

    @override
    int get hashCode =>
        enabled.hashCode +
        shareable.hashCode;

  factory BriefingInputSite.fromJson(Map<String, dynamic> json) => _$BriefingInputSiteFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingInputSiteToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

