//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_archive.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleArchive {
  /// Returns a new [ChronicleArchive] instance.
  ChronicleArchive({

    required  this.state,

     this.exportId,

     this.archivedAt,

     this.bytes,

     this.error,
  });

  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final ChronicleArchiveStateEnum state;



      /// ULID identifier
  @JsonKey(
    
    name: r'exportId',
    required: false,
    includeIfNull: false,
  )


  final String? exportId;



  @JsonKey(
    
    name: r'archivedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? archivedAt;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleArchive &&
      other.state == state &&
      other.exportId == exportId &&
      other.archivedAt == archivedAt &&
      other.bytes == bytes &&
      other.error == error;

    @override
    int get hashCode =>
        state.hashCode +
        (exportId == null ? 0 : exportId.hashCode) +
        (archivedAt == null ? 0 : archivedAt.hashCode) +
        (bytes == null ? 0 : bytes.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory ChronicleArchive.fromJson(Map<String, dynamic> json) => _$ChronicleArchiveFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleArchiveToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleArchiveStateEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'archived')
archived(r'archived'),
@JsonValue(r'restoring')
restoring(r'restoring'),
@JsonValue(r'failed')
failed(r'failed');

const ChronicleArchiveStateEnum(this.value);

final String value;

@override
String toString() => value;
}


