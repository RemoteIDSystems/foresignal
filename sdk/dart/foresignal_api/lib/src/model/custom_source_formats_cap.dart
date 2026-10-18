//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats_cap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormatsCap {
  /// Returns a new [CustomSourceFormatsCap] instance.
  CustomSourceFormatsCap({

     this.acceptNonActual = false,
  });

  @JsonKey(
    defaultValue: false,
    name: r'acceptNonActual',
    required: false,
    includeIfNull: false,
  )


  final bool? acceptNonActual;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormatsCap &&
      other.acceptNonActual == acceptNonActual;

    @override
    int get hashCode =>
        acceptNonActual.hashCode;

  factory CustomSourceFormatsCap.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsCapFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsCapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

