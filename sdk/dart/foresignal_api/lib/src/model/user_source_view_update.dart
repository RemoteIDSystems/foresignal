//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/user_source_pref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_source_view_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserSourceViewUpdate {
  /// Returns a new [UserSourceViewUpdate] instance.
  UserSourceViewUpdate({

    required  this.sources,
  });

  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<UserSourcePref> sources;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserSourceViewUpdate &&
      other.sources == sources;

    @override
    int get hashCode =>
        sources.hashCode;

  factory UserSourceViewUpdate.fromJson(Map<String, dynamic> json) => _$UserSourceViewUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$UserSourceViewUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

