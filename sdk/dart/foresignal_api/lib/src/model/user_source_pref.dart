//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_source_pref.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserSourcePref {
  /// Returns a new [UserSourcePref] instance.
  UserSourcePref({

    required  this.sourceId,

    required  this.enabled,

     this.displayName,

     this.color,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



      /// Whether this source appears in the caller's views, alerts and maps
  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Optional per-user label override
  @JsonKey(
    
    name: r'displayName',
    required: false,
    includeIfNull: false,
  )


  final String? displayName;



      /// Optional per-user display color
  @JsonKey(
    
    name: r'color',
    required: false,
    includeIfNull: false,
  )


  final String? color;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserSourcePref &&
      other.sourceId == sourceId &&
      other.enabled == enabled &&
      other.displayName == displayName &&
      other.color == color;

    @override
    int get hashCode =>
        sourceId.hashCode +
        enabled.hashCode +
        (displayName == null ? 0 : displayName.hashCode) +
        (color == null ? 0 : color.hashCode);

  factory UserSourcePref.fromJson(Map<String, dynamic> json) => _$UserSourcePrefFromJson(json);

  Map<String, dynamic> toJson() => _$UserSourcePrefToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

