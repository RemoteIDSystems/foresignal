//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'glance_activity.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GlanceActivity {
  /// Returns a new [GlanceActivity] instance.
  GlanceActivity({

     this.versionsLastHour,
  });

  @JsonKey(
    
    name: r'versionsLastHour',
    required: false,
    includeIfNull: false,
  )


  final int? versionsLastHour;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GlanceActivity &&
      other.versionsLastHour == versionsLastHour;

    @override
    int get hashCode =>
        versionsLastHour.hashCode;

  factory GlanceActivity.fromJson(Map<String, dynamic> json) => _$GlanceActivityFromJson(json);

  Map<String, dynamic> toJson() => _$GlanceActivityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

