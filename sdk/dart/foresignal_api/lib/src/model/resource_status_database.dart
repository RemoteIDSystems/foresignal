//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resource_status_database.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResourceStatusDatabase {
  /// Returns a new [ResourceStatusDatabase] instance.
  ResourceStatusDatabase({

     this.sizeBytes,
  });

  @JsonKey(
    
    name: r'sizeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResourceStatusDatabase &&
      other.sizeBytes == sizeBytes;

    @override
    int get hashCode =>
        (sizeBytes == null ? 0 : sizeBytes.hashCode);

  factory ResourceStatusDatabase.fromJson(Map<String, dynamic> json) => _$ResourceStatusDatabaseFromJson(json);

  Map<String, dynamic> toJson() => _$ResourceStatusDatabaseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

