//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_database.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoDatabase {
  /// Returns a new [SystemInfoDatabase] instance.
  SystemInfoDatabase({

     this.status,

     this.sizeBytes,

     this.postgisVersion,
  });

  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoDatabaseStatusEnum? status;



  @JsonKey(
    
    name: r'sizeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBytes;



  @JsonKey(
    
    name: r'postgisVersion',
    required: false,
    includeIfNull: false,
  )


  final String? postgisVersion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoDatabase &&
      other.status == status &&
      other.sizeBytes == sizeBytes &&
      other.postgisVersion == postgisVersion;

    @override
    int get hashCode =>
        status.hashCode +
        sizeBytes.hashCode +
        postgisVersion.hashCode;

  factory SystemInfoDatabase.fromJson(Map<String, dynamic> json) => _$SystemInfoDatabaseFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoDatabaseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SystemInfoDatabaseStatusEnum {
@JsonValue(r'up')
up(r'up'),
@JsonValue(r'down')
down(r'down');

const SystemInfoDatabaseStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


