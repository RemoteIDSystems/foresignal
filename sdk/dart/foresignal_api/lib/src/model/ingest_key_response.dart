//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_key_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestKeyResponse {
  /// Returns a new [IngestKeyResponse] instance.
  IngestKeyResponse({

    required  this.ingestKey,
  });

      /// Shown once
  @JsonKey(
    
    name: r'ingestKey',
    required: true,
    includeIfNull: false,
  )


  final String ingestKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestKeyResponse &&
      other.ingestKey == ingestKey;

    @override
    int get hashCode =>
        ingestKey.hashCode;

  factory IngestKeyResponse.fromJson(Map<String, dynamic> json) => _$IngestKeyResponseFromJson(json);

  Map<String, dynamic> toJson() => _$IngestKeyResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

