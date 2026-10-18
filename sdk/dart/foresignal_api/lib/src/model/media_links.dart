//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaLinks {
  /// Returns a new [MediaLinks] instance.
  MediaLinks({

     this.self,

     this.meta,

     this.thumbnail256,

     this.thumbnail1024,
  });

  @JsonKey(
    
    name: r'self',
    required: false,
    includeIfNull: false,
  )


  final String? self;



  @JsonKey(
    
    name: r'meta',
    required: false,
    includeIfNull: false,
  )


  final String? meta;



  @JsonKey(
    
    name: r'thumbnail256',
    required: false,
    includeIfNull: false,
  )


  final String? thumbnail256;



  @JsonKey(
    
    name: r'thumbnail1024',
    required: false,
    includeIfNull: false,
  )


  final String? thumbnail1024;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaLinks &&
      other.self == self &&
      other.meta == meta &&
      other.thumbnail256 == thumbnail256 &&
      other.thumbnail1024 == thumbnail1024;

    @override
    int get hashCode =>
        self.hashCode +
        meta.hashCode +
        thumbnail256.hashCode +
        thumbnail1024.hashCode;

  factory MediaLinks.fromJson(Map<String, dynamic> json) => _$MediaLinksFromJson(json);

  Map<String, dynamic> toJson() => _$MediaLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

