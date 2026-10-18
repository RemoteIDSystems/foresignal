//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats_cloudevents.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormatsCloudevents {
  /// Returns a new [CustomSourceFormatsCloudevents] instance.
  CustomSourceFormatsCloudevents({

     this.typeKinds,
  });

      /// CloudEvents `type` → kind; else the single kind, else rejected
  @JsonKey(
    
    name: r'typeKinds',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? typeKinds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormatsCloudevents &&
      other.typeKinds == typeKinds;

    @override
    int get hashCode =>
        typeKinds.hashCode;

  factory CustomSourceFormatsCloudevents.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsCloudeventsFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsCloudeventsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

