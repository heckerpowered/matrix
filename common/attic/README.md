# attic

Code parked here is **excluded from compilation**.

- `particle/`, `ParticleRenderer.kt` — the GPU particle system, retired during the 26.2
  migration. It was built directly on OpenGL (VAO/VBO double buffering, transform feedback)
  and has no equivalent in the 26.2 `GpuDevice` wrapper API. Call sites in
  `ScreenEffectRenderer`, `FinderArrowEntityRenderer` and `ClientboundExplosionPayload`
  are commented out and marked with `GPU particle system retired (see common/attic)`.

- `client/shader/{Program,Shader,ResourceShader,ShaderCompiler,ShaderCompilerV1,
  ShaderStageStore,ShaderSourceDescriptor,ShaderExecutable,ShaderStage,Uniform,
  UniformLocation,UniformWriter,FloatUniform,Vector2fUniform,Vector3fUniform,
  Vector4fUniform,IntUniform,Matrix4fUniform,MonolithicProgramUniformWriter,
  SeparableProgramUniformWriter,UniformBufferProvider}.kt`, `client/shader/component/`,
  `client/shader/cache/` — the pre-26.2 mesh-shader compilation/linking/uniform-writer
  infrastructure (raw `glCreateProgram`/`glAttachShader`/`glUniform*`, per-pointer
  `UniformProvider(name) { pointer -> ... }`). Superseded by
  `heckerpowered.matrix.client.shader.BlitProgram` + the new std140-block
  `UniformProvider`/`TextureProvider` (see `BlitProgram.kt`/`UniformProvider.kt`), which
  compiles GLSL through the vanilla `ShaderManager` and has no concept of a globally bound
  program. There is no wrapper-API equivalent for "bind this program globally, then let
  unrelated immediate-mode/vertex-attribute mesh code draw against it" — callers that relied
  on that (`DissolveShader.enableShader/disableShader`, `PositionColorProgram`,
  `MagicList.kt`, `ManaBar.kt`, `MatrixHud.kt`) either dropped the mesh-shader path or are
  pending rework by whoever owns those files.

- `client/render/{OpenGLExtensions,FramebufferCapture,AdvancedFramebuffer,
  GpuPerformanceCounter}.kt`, `client/render/state/` (whole dir, including
  `state/capabilities/`) — raw-GL debugging/state-capture/state-isolation helpers
  (`glGetError`, GL capability snapshot+restore, `StateIsolation.isolate { }` wrappers).
  On the 26.2 wrapper API blend/depth state is baked into `RenderPipeline`s per draw call
  and render passes are self-contained, so there is nothing left to snapshot/restore around
  a draw; callers that wrapped draws in `StateIsolation.isolate(...)` had the wrapper
  removed and the blend/depth args translated into `BlitProgram.drawTo`'s `blend` parameter
  where applicable.
