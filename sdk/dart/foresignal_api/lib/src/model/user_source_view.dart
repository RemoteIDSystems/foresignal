//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/user_source_pref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_source_view.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserSourceView {
  /// Returns a new [UserSourceView] instance.
  UserSourceView({

    required  this.sources,
  });

  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<UserSourcePref> sources;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserSourceView &&
      other.sources == sources;

    @override
    int get hashCode =>
        sources.hashCode;

  factory UserSourceView.fromJson(Map<String, dynamic> json) => _$UserSourceViewFromJson(json);

  Map<String, dynamic> toJson() => _$UserSourceViewToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

