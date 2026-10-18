//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/external_source.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_source_created.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalSourceCreated {
  /// Returns a new [ExternalSourceCreated] instance.
  ExternalSourceCreated({

    required  this.source_,

    required  this.ingestKey,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final ExternalSource source_;



      /// eik_… (shown once)
  @JsonKey(
    
    name: r'ingestKey',
    required: true,
    includeIfNull: false,
  )


  final String ingestKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalSourceCreated &&
      other.source_ == source_ &&
      other.ingestKey == ingestKey;

    @override
    int get hashCode =>
        source_.hashCode +
        ingestKey.hashCode;

  factory ExternalSourceCreated.fromJson(Map<String, dynamic> json) => _$ExternalSourceCreatedFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalSourceCreatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

