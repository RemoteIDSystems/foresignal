//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_rasters_glm.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemRastersGlm {
  /// Returns a new [SystemRastersGlm] instance.
  SystemRastersGlm({

     this.ringFlashes,

     this.archiveBytes,

     this.newestFileAt,
  });

  @JsonKey(
    
    name: r'ringFlashes',
    required: false,
    includeIfNull: false,
  )


  final int? ringFlashes;



  @JsonKey(
    
    name: r'archiveBytes',
    required: false,
    includeIfNull: false,
  )


  final int? archiveBytes;



  @JsonKey(
    
    name: r'newestFileAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? newestFileAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemRastersGlm &&
      other.ringFlashes == ringFlashes &&
      other.archiveBytes == archiveBytes &&
      other.newestFileAt == newestFileAt;

    @override
    int get hashCode =>
        ringFlashes.hashCode +
        archiveBytes.hashCode +
        (newestFileAt == null ? 0 : newestFileAt.hashCode);

  factory SystemRastersGlm.fromJson(Map<String, dynamic> json) => _$SystemRastersGlmFromJson(json);

  Map<String, dynamic> toJson() => _$SystemRastersGlmToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

