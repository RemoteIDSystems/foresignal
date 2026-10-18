//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_holds.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoHolds {
  /// Returns a new [SystemInfoHolds] instance.
  SystemInfoHolds({

     this.count,

     this.rows,

     this.bytes,
  });

  @JsonKey(
    
    name: r'count',
    required: false,
    includeIfNull: false,
  )


  final int? count;



  @JsonKey(
    
    name: r'rows',
    required: false,
    includeIfNull: false,
  )


  final int? rows;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoHolds &&
      other.count == count &&
      other.rows == rows &&
      other.bytes == bytes;

    @override
    int get hashCode =>
        count.hashCode +
        rows.hashCode +
        bytes.hashCode;

  factory SystemInfoHolds.fromJson(Map<String, dynamic> json) => _$SystemInfoHoldsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoHoldsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

